package com.bbcms.web;

import com.bbcms.annotation.PermissionGroup;
import com.bbcms.domain.ScreenDomain;
import com.bbcms.exception.ErrorCode;
import com.bbcms.exception.ErrorResponse;
import com.bbcms.model.Screen;
import com.bbcms.service.ScreenService;
import com.bbcms.util.JsonUtils;
import com.cloud.sdk.DefaultRequest;
import com.cloud.sdk.Request;
import com.cloud.sdk.auth.credentials.BasicCredentials;
import com.cloud.sdk.auth.signer.DefaultSigner;
import com.cloud.sdk.auth.signer.Signer;
import com.cloud.sdk.auth.signer.SignerFactory;
import com.cloud.sdk.util.IOUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/assk")
public class AsskAPI extends BaseAPI {


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String save(HttpServletRequest httpServletRequest) throws IOException {
        Signer signer = SignerFactory.getSigner("", "");
        Request request1 = generateRequest(httpServletRequest);
        signer.sign(request1, new BasicCredentials("abcdeg2342abcdeg2342abcdeg2342ok", "opkuht2342abcdeg2342abcdeg2342ok"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String authorization = (String)request1.getHeaders().get("Authorization");
        String xsdkdate = (String)request1.getHeaders().get("X-Sdk-Date");
        System.out.println(authorization);
        System.out.println(xsdkdate);
        DefaultSigner defaultSigner = (DefaultSigner) signer;
        Verify verify = new Verify();

        Request request2 = generateRequest(httpServletRequest);
        request2.addHeader("Authorization",authorization);
        request2.addHeader("X-Sdk-Date",xsdkdate);

        boolean verify1 = verify.verify(request2, new BasicCredentials("abcdeg2342abcdeg2342abcdeg2342ok", "opkuht2342abcdeg2342abcdeg2342ok"));
        System.out.println(verify1);
        return authorization;
    }

    public Request generateRequest(HttpServletRequest httpServletRequest) throws IOException {

        // Make a request for signing.
        Request request = new DefaultRequest("foo");
        try {
            URL url = new URL(httpServletRequest.getRequestURL().toString());
            // Set the request address.
            request.setEndpoint(url.toURI());

            String urlString = url.toString();

            String parameters = null;

            if (urlString.contains("?")) {
                parameters = urlString.substring(urlString.indexOf("?") + 1);
                Map parametersmap = new HashMap<String, String>();

                if (null != parameters && !"".equals(parameters)) {
                    String[] parameterarray = parameters.split("&");

                    for (String p : parameterarray) {
                        String key = p.split("=")[0];
                        String value = p.split("=")[1];
                        parametersmap.put(key, value);
                    }
                    request.setParameters(parametersmap);
                }
            }

        } catch (URISyntaxException e) {
            // It is recommended to add logs in this place.
            e.printStackTrace();
        }
        request.setHttpMethod(request.getHttpMethod());

        InputStream content = new ByteArrayInputStream(IOUtils.toString(httpServletRequest.getInputStream()).getBytes());

        System.out.println(IOUtils.toString(content));
        request.setContent(content);
        return request;

    }

    private static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
}
