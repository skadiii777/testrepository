package com.enterprise.module.biz.controller.admin.payment.vo.payment;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class PaymentReverseReqVO {
    @NotNull private Long id;
    @NotBlank @Size(max = 200) private String reason;
}
