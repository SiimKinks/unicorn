package com.siimkinks.unicorn;

public class EqualMockView extends MockView {

    public static EqualMockView createNewView() {
        return new EqualMockView();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) || o.getClass().getSimpleName().equals(getClass().getSimpleName());
    }
}
