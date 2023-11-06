package cc.allio.uno.turbo.common.exception;

import lombok.Getter;

/**
 * 通用业务异常
 *
 * @author j.x
 * @date 2023/10/23 12:52
 * @since 1.0.0
 */
public class BizException extends Exception {

    @Getter
    private String i18nCode;

    public BizException() {
    }

    public BizException(String message) {
        super(message);
    }

    /**
     * 创建turbo统一异常，它将会使用i18n作为国际化的异常消息处理，如果未找到国际化的信息，则使用提供的默认消息
     *
     * @param i18nCode       i18nCode
     * @param defaultMessage defaultMessage
     */
    public BizException(String i18nCode, String defaultMessage) {
        super(defaultMessage);
        this.i18nCode = i18nCode;
    }

}
