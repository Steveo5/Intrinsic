package com.hotmail.intrinsic;

import com.hotmail.intrinsic.menubuilder.MenuBuilder;

public class MainMenu extends MenuBuilder {
    public MainMenu(String name) {
        super(name);

        title("Welcome to your region terminal").size(9);
    }
}
