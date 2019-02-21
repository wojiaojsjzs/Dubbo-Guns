package com.stylefeng.guns.rest.modular.vo;

import lombok.Data;

@Data
public class ResponseVO<M> {
    //返回状态
    private int status;
    //返回信息
    private String msg;
    //返回数据实体
    private M data;
    //图片前缀
    private String imgPre;
    //
    private int nowPage;
    private int totalPage;
    private ResponseVO(){
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public M getData() {
        return data;
    }

    public void setData(M data) {
        this.data = data;
    }

    public static<M> ResponseVO success(int nowPage,int totalPage,String imgPre,M m){
        ResponseVO  responseVO = new ResponseVO();
        responseVO.setStatus(0);
        responseVO.setData(m);
        responseVO.setImgPre(imgPre);
        responseVO.setNowPage(nowPage);
        responseVO.setTotalPage(totalPage);
        return responseVO;
    }

    public static<M> ResponseVO success(M m){
        ResponseVO  responseVO = new ResponseVO();
        responseVO.setStatus(0);
        responseVO.setData(m);
        return responseVO;
    }
    public static<M> ResponseVO serviceFail(String message){
        ResponseVO  responseVO = new ResponseVO();
        responseVO.setStatus(1);
        responseVO.setMsg(message);
        return responseVO;
    }
    public static<M> ResponseVO appFail(String message){
        ResponseVO  responseVO = new ResponseVO();
        responseVO.setStatus(999);
        responseVO.setMsg(message);
        return responseVO;
    }
    public static<M> ResponseVO success(String message){
        ResponseVO  responseVO = new ResponseVO();
        responseVO.setStatus(0);
        responseVO.setMsg(message);
        return responseVO;
    }

    public static<M> ResponseVO success(String imgPre,M m){
        ResponseVO  responseVO = new ResponseVO();
        responseVO.setStatus(0);
        responseVO.setData(m);
        responseVO.setImgPre(imgPre);
        return responseVO;
    }
}
