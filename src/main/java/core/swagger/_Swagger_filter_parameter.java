package core.swagger;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public abstract class _Swagger_filter_parameter {

    @ApiModelProperty(value = "number of objects per page, min value is 1, max value is 50, default is 25. It's optional", required = false)
    @Constraints.Max(50)
    @Constraints.Min(1)
    public Integer count_on_page = 25;

}
