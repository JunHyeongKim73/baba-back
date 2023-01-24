package com.baba.back.oauth.domain.member;

import com.baba.back.oauth.domain.ColorPicker;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Icon {

    @Embedded
    private IconColor iconColor;

    @Embedded
    private IconName iconName;

    public static Icon of(ColorPicker<String> colorPicker, String iconName) {
        return new Icon(IconColor.from(colorPicker), new IconName(iconName));
    }
}