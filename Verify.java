package com.bbcms.web;

import com.cloud.sdk.Request;
import com.cloud.sdk.auth.credentials.Credentials;
import com.cloud.sdk.auth.signer.DefaultSigner;
import com.cloud.sdk.auth.signer.SigningAlgorithm;
import com.cloud.sdk.auth.signer.internal.SignerKey;
import com.cloud.sdk.auth.signer.internal.SignerRequestParams;
import com.cloud.sdk.internal.FIFOCache;
import com.cloud.sdk.util.BinaryUtils;
import com.cloud.sdk.util.DateUtils;
import com.cloud.sdk.util.StringUtils;

/**
 * Created by ligen on 17/1/14.
 */
public class Verify extends DefaultSigner {

    private static final FIFOCache<SignerKey> signerCache = new FIFOCache(300);

    public boolean verify(Request<?> request, Credentials credentials) {
        Credentials sanitizedCredentials = this.sanitizeCredentials(credentials);
        String singerDate = (String)request.getHeaders().get("X-Sdk-Date");
        String authorization = (String)request.getHeaders().remove("Authorization");
        SignerRequestParams signerParams = new SignerRequestParams(request, this.regionName, this.serviceName, "SDK-HMAC-SHA256", singerDate);
        this.addHostHeader(request);
        String contentSha256 = this.calculateContentHash(request);
        if("required".equals(request.getHeaders().get("x-sdk-content-sha256"))) {
            request.addHeader("x-sdk-content-sha256", contentSha256);
        }

        String canonicalRequest = this.createCanonicalRequest(request, contentSha256);
        String stringToSign = this.createStringToSign(canonicalRequest, signerParams);
        byte[] signingKey = this.deriveSigningKey(sanitizedCredentials, signerParams);
        byte[] signature = this.computeSignature(stringToSign, signingKey, signerParams);
        String signatureResult = this.buildAuthorizationHeader(request, signature, sanitizedCredentials, signerParams);
        System.out.println(signatureResult);
        return signatureResult.equals(authorization);
    }

    private final byte[] deriveSigningKey(Credentials credentials, SignerRequestParams signerRequestParams) {
        String cacheKey = this.computeSigningCacheKeyName(credentials, signerRequestParams);
        long daysSinceEpochSigningDate = DateUtils.numberOfDaysSinceEpoch(signerRequestParams.getSigningDateTimeMilli());
        SignerKey signerKey = (SignerKey)signerCache.get(cacheKey);
        if(signerKey != null && daysSinceEpochSigningDate == signerKey.getNumberOfDaysSinceEpoch()) {
            return signerKey.getSigningKey();
        } else {
            byte[] signingKey = this.newSigningKey(credentials, signerRequestParams.getFormattedSigningDate(), signerRequestParams.getRegionName(), signerRequestParams.getServiceName());
            signerCache.add(cacheKey, new SignerKey(daysSinceEpochSigningDate, signingKey));
            return signingKey;
        }
    }

    private final String computeSigningCacheKeyName(Credentials credentials, SignerRequestParams signerRequestParams) {
        StringBuilder hashKeyBuilder = new StringBuilder(credentials.getSecretKey());
        return hashKeyBuilder.append("-").append(signerRequestParams.getRegionName()).append("-").append(signerRequestParams.getServiceName()).toString();
    }

    private String buildAuthorizationHeader(Request<?> request, byte[] signature, Credentials credentials, SignerRequestParams signerParams) {
        String signingCredentials = credentials.getAccessKeyId() + "/" + signerParams.getScope();
        String credential = "Credential=" + signingCredentials;
        String signerHeaders = "SignedHeaders=" + this.getSignedHeadersString(request);
        String signatureHeader = "Signature=" + BinaryUtils.toHex(signature);
        StringBuilder authHeaderBuilder = new StringBuilder();
        authHeaderBuilder.append("SDK-HMAC-SHA256").append(" ").append(credential).append(", ").append(signerHeaders).append(", ").append(signatureHeader);
        return authHeaderBuilder.toString();
    }

    private byte[] newSigningKey(Credentials credentials, String dateStamp, String regionName, String serviceName) {
        byte[] kSecret = ("SDK" + credentials.getSecretKey()).getBytes(StringUtils.UTF8);
        byte[] kDate = this.sign(dateStamp, kSecret, SigningAlgorithm.HmacSHA256);
        byte[] kRegion = this.sign(regionName, kDate, SigningAlgorithm.HmacSHA256);
        byte[] kService = this.sign(serviceName, kRegion, SigningAlgorithm.HmacSHA256);
        return this.sign("sdk_request", kService, SigningAlgorithm.HmacSHA256);
    }
}
