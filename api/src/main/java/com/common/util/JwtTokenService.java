package com.common.util;

import com.common.domain.PayloadDto;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.KeyLengthException;

import java.text.ParseException;

public interface JwtTokenService {

    /**
     * 创建token
     * @param payloadStr 负载信息
     * @param secret 盐
     * @return
     */
    String generateTokenByHMAC(String payloadStr, String secret) throws JOSEException;

    /**
     * 解密token
     * @param token
     * @param secret
     * @return
     */
    PayloadDto verifyTokenByHMAC(String token, String secret) throws ParseException, JOSEException;

    PayloadDto getDefaultPayloadDto();
}
