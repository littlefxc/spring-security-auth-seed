/**
 *
 */
package com.fengxuechao.seed.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author fengxuechao
 * @date 2019-09-11
 */
@ConfigurationProperties(prefix = "seed.security")
public class SecurityProperties {

    /**
     * 浏览器环境配置
     */
    private BrowserProperties browser = new BrowserProperties();
    /**
     * 验证码配置
     */
//	private ValidateCodeProperties code = new ValidateCodeProperties();
    /**
     * 社交登录配置
     */
//	private SocialProperties social = new SocialProperties();
    /**
     * OAuth2认证服务器配置
     */
//	private OAuth2Properties oauth2 = new OAuth2Properties();

    public BrowserProperties getBrowser() {
        return browser;
    }

    public void setBrowser(BrowserProperties browser) {
        this.browser = browser;
    }
}

