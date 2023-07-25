package com.infomaximum.im_event.Components;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

/**
 * Created by a.kiperku
 * Date: 25.07.2023
 */
@Data
public class TGInlineButton extends InlineKeyboardButton{

    private int row;

    public TGInlineButton(String text, String data, int row){
        this.setText(text);
        this.setCallbackData(data);
        this.row = row - 1;
    }


}
