package com.enterprise.module.biz.controller.admin.correction.vo.correction;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.enterprise.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 补卡申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AttendanceCorrectionPageReqVO extends PageParam {

    @Schema(description = "员工姓名")
    private String empName;

    @Schema(description = "补卡日期")
    private String workDate;

    @Schema(description = "补卡类型（1=补上班卡 2=补下班卡）")
    private String correctType;

    @Schema(description = "审批状态（0待审批 1已通过 2已驳回）")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
