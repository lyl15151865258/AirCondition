package com.zhongbenshuo.air.network;

import com.zhongbenshuo.air.bean.OpenAndCloseDoorRecord;
import com.zhongbenshuo.air.bean.Result;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Retrofit网络请求构建接口
 * Created at 2018/11/28 13:48
 *
 * @author LiYuliang
 * @version 1.0
 */

public interface ZbsApi {

    /**
     * 查询服务器RSA公钥
     *
     * @return 返回值
     */
    @GET("user/rsaPublicKey.do")
    Observable<Result> getRSAPublicKey();

    /**
     * 远程开关门
     *
     * @return 返回值
     */
    @POST("user/openAndCloseDoorRecord.do")
    Observable<Result> openAndCloseDoorRecord(@Body OpenAndCloseDoorRecord params);

    /**
     * 下载软件
     *
     * @return ResponseBody
     */
    @GET("VersionController/downloadNewVersion.do")
    Call<ResponseBody> downloadFile();

    /**
     * 下载软件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    @GET
    Call<ResponseBody> downloadFile(@Url String filePath);

}
