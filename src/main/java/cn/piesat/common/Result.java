package cn.piesat.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "返回说明")
public class Result {

    @ApiModelProperty(value = "返回错误状态码；0:成功,其他表示相应的失败")
    private Integer code;
    @ApiModelProperty(value = "信息")
    private String message;
    @ApiModelProperty(value = "数据")
    private Object data;

    public Result(Integer code){
        this.code = code;
    }

    public Result(Integer code,String message){
        this.code = code;
        this.message = message;
    }

    public Result(){

    }
}
