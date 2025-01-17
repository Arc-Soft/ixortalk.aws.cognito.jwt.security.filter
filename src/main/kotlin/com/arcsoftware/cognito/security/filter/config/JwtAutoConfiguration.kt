/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-present IxorTalk CVBA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.arcsoftware.cognito.security.filter.config


import com.arcsoftware.cognito.security.filter.filter.AwsCognitoIdTokenProcessor
import com.arcsoftware.cognito.security.filter.filter.AwsCognitoJwtAuthenticationFilter
import com.nimbusds.jose.JWSAlgorithm.RS256
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jose.util.DefaultResourceRetriever
import com.nimbusds.jose.util.ResourceRetriever
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import java.net.MalformedURLException
import java.net.URL

/**
 *
 * Our auto configuration class that exposes
 *
 * - CredentialHolder
 * - JWTProcessor
 * - AuthenticationProvider
 * - JWTAuthenticationFilter
 * - AwsCognitoJtwConfiguration
 *
 */
@Configuration
@ConditionalOnClass(AwsCognitoJwtAuthenticationFilter::class, AwsCognitoIdTokenProcessor::class)
@EnableConfigurationProperties(JwtConfiguration::class)
class JwtAutoConfiguration {

    @Autowired
    private val jwtConfiguration: JwtConfiguration? = null

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    fun awsCognitoCredentialsHolder(): JwtIdTokenCredentialsHolder {
        return JwtIdTokenCredentialsHolder()
    }

    @Bean
    fun awsCognitoIdTokenProcessor(): AwsCognitoIdTokenProcessor {
        return AwsCognitoIdTokenProcessor()
    }

    @Bean
    fun jwtAuthenticationProvider(): JwtAuthenticationProvider {
        return JwtAuthenticationProvider()
    }


    @Bean
    fun awsCognitoJwtAuthenticationFilter(): AwsCognitoJwtAuthenticationFilter {
        return AwsCognitoJwtAuthenticationFilter(awsCognitoIdTokenProcessor())
    }

    @Bean
    @Throws(MalformedURLException::class)
    fun configurableJWTProcessor(): ConfigurableJWTProcessor<*> {
        val resourceRetriever: ResourceRetriever = DefaultResourceRetriever(jwtConfiguration!!.connectionTimeout, jwtConfiguration.readTimeout)
        val jwkSetURL = URL(jwtConfiguration.jwkUrl)
        val keySource = RemoteJWKSet<SecurityContext>(jwkSetURL, resourceRetriever)
        val jwtProcessor = DefaultJWTProcessor<SecurityContext>()
        val keySelector = JWSVerificationKeySelector(RS256, keySource)
        jwtProcessor.jwsKeySelector = keySelector
        return jwtProcessor
    }

}