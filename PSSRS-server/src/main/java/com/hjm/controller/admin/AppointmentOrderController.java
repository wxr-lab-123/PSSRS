package com.hjm.controller.admin;


import com.hjm.pojo.DTO.AppointmentOrderPageDTO;
import com.hjm.pojo.DTO.RegistrationCancelDTO;
import com.hjm.pojo.Entity.AppointmentOrder;
import com.hjm.result.PageResult;
import com.hjm.result.Result;
import com.hjm.service.IAppointmentOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 挂号订单表 前端控制器
 * </p>
 *
 * @author hjm
 * @since 2025-10-31
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AppointmentOrderController {

    private  final IAppointmentOrderService appointmentOrderService;

    @GetMapping("/registrations")
    public Result<PageResult> list(AppointmentOrderPageDTO appointmentOrderPageDTO) {
        return appointmentOrderService.listByPage(appointmentOrderPageDTO);
    }

    @PutMapping("/registrations/{id}/cancel")
    public Result cancel(@PathVariable Integer id,@RequestBody RegistrationCancelDTO DTO) {
        AppointmentOrder order = appointmentOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setStatus(2);
        order.setCancelReason(DTO.getReason());
        order.setRemark(DTO.getRemark());
        boolean update = appointmentOrderService.updateById(order);
        if (!update) {
            return Result.error("取消订单失败");
        }
        return Result.success("取消订单成功");
    }

}
