package com.common.util;


import com.alibaba.fastjson2.JSON;
import com.common.domain.PayloadDto;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import org.assertj.core.util.DateUtil;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * jwt工具类
 */
@Service
public class JwtUtil implements JwtTokenService {


    /**
     * 生成token
     * @param payloadStr 负载信息
     * @param secret 盐
     * @return
     * @throws JOSEException
     */
    @Override
    public String generateTokenByHMAC(String payloadStr, String secret) throws JOSEException {
        //创建JWS头，设置签名算法和类型
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.HS256).
                type(JOSEObjectType.JWT)
                .build();
        //将负载信息封装到Payload中
        Payload payload = new Payload(payloadStr);
        //创建JWS对象
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        //创建HMAC签名器
        JWSSigner jwsSigner = new MACSigner(secret);
        //签名
        jwsObject.sign(jwsSigner);
        return jwsObject.serialize();
    }

    @Override
    public PayloadDto verifyTokenByHMAC(String token, String secret) throws ParseException, JOSEException {
        //从token中解析JWS对象
        JWSObject jwsObject = JWSObject.parse(token);
        //创建HMAC验证器
        JWSVerifier jwsVerifier = new MACVerifier(secret);
        if (!jwsObject.verify(jwsVerifier)) {
            throw new RuntimeException("token签名不合法！");
        }
        String payload = jwsObject.getPayload().toString();
        PayloadDto payloadDto = JSON.parseObject(payload, PayloadDto.class);
        if (payloadDto.getExp() < new Date().getTime()) {
            throw new RuntimeException("token已过期！");
        }
        return payloadDto;
    }

    @Override
    public PayloadDto getDefaultPayloadDto() {
        //现在时间毫秒数
        long now = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        //过期时间
        long exp = LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return PayloadDto.builder()
                .sub("macro")
                .iat(now)
                .exp(exp)
                .jti(UUID.randomUUID().toString())
                .username("macro")
                .authorities(Arrays.asList("ADMIN"))
                .build();
    }
}
