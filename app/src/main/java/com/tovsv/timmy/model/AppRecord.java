package com.tovsv.timmy.model;


import com.google.gson.Gson;
import com.tovsv.timmy.app.Constants;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.QueryResult;
import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;

import com.tovsv.timmy.util.AppActionHelper;
import com.tovsv.timmy.util.DLog;

/**
 * Created by kleist on 14-5-24.
 */
//@Table("app_record")
public class AppRecord implements QueryResult{

    @AutoIncrementPrimaryKey
    @Column("id")
    public long id;

    @Column("package_name")
    public String packageName;

    @Column("sum(duration)")
    public long duration;
}
