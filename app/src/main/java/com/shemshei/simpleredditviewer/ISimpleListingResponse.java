package com.shemshei.simpleredditviewer;

import java.util.List;

/**
 * Created by romanshemshei on 5/30/17.
 */

public interface ISimpleListingResponse<T extends Object> {

    void setKind(String kind);

    String getKind();

    void setData(List<T> data);

    T getData();

}
