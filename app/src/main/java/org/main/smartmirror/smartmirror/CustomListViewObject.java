package org.main.smartmirror.smartmirror;

import android.net.Uri;

public class CustomListViewObject {

    private String prop1;
    private String prop2;
    private Uri prop3;
    private String prop4;

    public CustomListViewObject(String prop1, String prop2, Uri prop3, String prop4) {
        this.prop1 = prop1;
        this.prop2 = prop2;
        this.prop3 = prop3;
        this.prop4 = prop4;
    }

    public String getProp1() {
        return prop1;
    }

    public String getProp2() {
        return prop2;
    }

    public Uri getProp3() {
        return prop3;
    }

    public String getProp4() { return prop4; }
}
