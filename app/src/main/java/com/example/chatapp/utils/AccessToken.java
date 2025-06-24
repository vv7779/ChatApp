package com.example.chatapp.utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AccessToken {

  private static final String firebaseMessagingScope =
      "https://www.googleapis.com/auth/firebase.messaging";

  public static String getAccessToken() {

    try {

      String jsonString =
          "{\n"
              + "  \"type\": \"service_account\",\n"
              + "  \"project_id\": \"chat-app-backend-project\",\n"
              + "  \"private_key_id\": \"8a17e055aed3d6a1154fe6bbff1503a3d43d0627\",\n"
              + "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCwT2o7VkU5pjBs\\ngZmkiU6uQnaugCjtx+n52yjFDMKoz7tNZ6yF/QqzpmCepr7yB/xOfYVx6iNKU39C\\ntVk80SxDGs7pUFWhRclfucWyxoi7oTs2UeKbs0iOor1e+p0BOb9K/QAfBkR+qGur\\n202sB8SCDGLE6eGRac2e2bjtxHxPzt884wnRPwez/9k5f/VGUEPPWDI9TKCoBOlq\\n8m8LV5W3j8L06W/KEu27mAn6vJxE3vBBXnt6M+Qi7Gz6EWat+uf/QUVwPQvriY09\\ny1FAwvLgAuYt8CKL8oDej/bkiwSVw5+CW0pel223qvSgcURovUmZ8KGWD6LCuTom\\nW+2l+I5vAgMBAAECggEAJTXFhltpRcb4zlW13v9pnyaA1MhQmwqHq4dg/eF5Dda1\\nwZrhUu/6O7sgBivL/dnImqosKUQQiaXApIwQluDQliMIyz38rIpFe1ICUv5KqkrX\\na9nxWQoIBK59CBgdXb5ZzJJD3chdWVvjSYSByo5jH01M5SE1TZLGZkGuIWRIFjGJ\\nL6mXQhRzcLky/8rS5SJ6TPzn3XrPe1Qn3c8uDtZKelHAIOnW2kiqdmHNcz/D+SMi\\nysu3Y+FMGN7tz0xtXD9Cx/9Me8D7tqPRl+rOqgrZ3k4HUFxkxbjopOnEZTLEK8NJ\\n4w6lxHU5bGCOMrF0LcacX6dBXFKhY2dk30FNSdQOhQKBgQDuWTpX4Ph+YX1fstm7\\nSHdW+8F3veVTSeNPVFVQuzavYXVlUm12J0qLd7jmrdzvjSKxcUE5UZe8X4x49eTs\\n4jQcaLA9czXcPae49OlrvnWh4/hKwnl8QiRl/P4erUO/zAuGdS/VTYa9P4U7klMg\\nMfyaqCKnRQLN2i4+ATFbjKcNqwKBgQC9XgXLT39uXInm2LEBFoo/K97SXdEPdEro\\nwl8eEnOp8O5yNC8Asc+wrOb/csspxaR2mUhjlUnNYhgB5LkzuD0VGVruaKCyN5zF\\nEP0X3TTEOYTtbHMogwVMvKy48k22SmjJSGMRus58mPHcWKFLfm4+bTd8MGt66Qc1\\nb3chVWZWTQKBgQCOWLPDcBR4Rfj+gm4Or8S2KjztKngfOe2Lny4kvOp365iVHplJ\\nC0Y17RZ8jjF4z618k8sEVHpughH+27wQzTeaD9vRl1D/a8MTbxFIy8KtIN1UqLit\\nQUNDtch+wbPIhDAN6Ubb+SUTOTB013rVq4TVSL0JcOmvzzH2zfeQWXwbaQKBgQCe\\nTXVGisSae52Eb2043aDgQkpZTbgw1SSyA5sERqSOUizjJ6CLUhmvNjs9HLedye08\\nf9M1/Fzf5rrvCkeQZtoZQ0LyvmBn23mgfB03z+IGj5cB64ExQqYxiiBR060HUJjc\\nezjmX2WyXyEA9M6Rj7WVVjPmC4wuCZPPJclPAlc0mQKBgA5ZzxZUQgEh/axKFU9l\\n/li2mWlVcGFYqNRuKG8IQ+NxDpy/h8W9fZli+/ScFE0oswuwwfjvmSa4yngnQJcz\\nhgFYVzo1howRqn1erzkHeO1Vy4t6pfErfmSOLKpBRCETnUXTdhpgZ4N4GHWOwe+7\\nhhDsGRr1oWs8JKtMLM8IaKNG\\n-----END PRIVATE KEY-----\\n\",\n"
              + "  \"client_email\": \"firebase-adminsdk-fbsvc@chat-app-backend-project.iam.gserviceaccount.com\",\n"
              + "  \"client_id\": \"106663162333103872251\",\n"
              + "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n"
              + "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n"
              + "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n"
              + "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40chat-app-backend-project.iam.gserviceaccount.com\",\n"
              + "  \"universe_domain\": \"googleapis.com\"\n"
              + "}\n";

        InputStream stream=new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

        GoogleCredentials googleCredentials=GoogleCredentials.fromStream(stream).createScoped(Lists.newArrayList(firebaseMessagingScope));

        googleCredentials.refresh();

        return googleCredentials.getAccessToken().getTokenValue();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
