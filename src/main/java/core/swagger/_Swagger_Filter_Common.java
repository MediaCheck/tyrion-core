package core.swagger;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by zaruba on 01.12.17.
 */
public abstract class _Swagger_Filter_Common extends _Swagger_Abstract_Default {

    /* Basic Filter Value --------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true, value = "First value position from all subjects. Minimum is 0.")
    public int from = 0;

    @ApiModelProperty(required = true, readOnly = true, value = "Minimum is \"from\" Maximum is \"total\"")
    public int to = 0;

    @ApiModelProperty(required = true, readOnly = true, value = "Total subjects")
    public int total = 0;

    @ApiModelProperty(required = true, readOnly = true, value = "Numbers of pages, which you can call")
    public int pages = 0;

}
