package com.smartdoc.chatModel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProviderEnum {
    DEEPSEEK("deepsekk"),
    ZHIPU("zhupu");

    private final String code;
}
