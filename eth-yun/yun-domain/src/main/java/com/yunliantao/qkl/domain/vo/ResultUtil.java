package com.yunliantao.qkl.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultUtil<T> implements Serializable {

    private Integer code;

    private String msg;

    private T data;

    private T total;


//    public ResultUtil(Integer code, String msg, T data) {
//        this.code = code;
//        this.msg = msg;
//        this.data = data;
//    }

    public ResultUtil(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultUtil(T data, T total) {
        this.data = data;
        this.total = total;
    }


    public ResultUtil() {
    }

    public ResultUtil(T data) {
        this.data = data;
    }

    public static <T> ResultUtil<T> ok(T data) {
        ResultUtil<T> result = new ResultUtil<T>(data);
        result.setCode(0);//操作成功
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    public static <T> ResultUtil ok(T data, T total) {
        ResultUtil<T> result = new ResultUtil<T>(data, total);
        result.setCode(0);//操作成功
        result.setMsg("success");
        return result;
    }


    public static <T> ResultUtil<T> result(Integer code, String msg, T data) {
        ResultUtil<T> result = new ResultUtil<T>(data);
        result.setCode(code);//操作成功
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> ResultUtil<T> fail(T data) {
        ResultUtil<T> result = new ResultUtil<T>();
        result.setCode(1);//操作失败
        result.setMsg((String) data);
        return result;
    }

    public static <T> ResultUtil<T> not_ok(String msg) {
        ResultUtil<T> result = new ResultUtil<T>();
        result.setCode(1);//操作失败
        result.setMsg(msg);
        return result;
    }


    public ResultUtil<T> msg(String msg) {
        this.setMsg(msg);
        return this;
    }

    public ResultUtil<T> code(Integer code) {
        this.setCode(code);
        return this;
    }


}
