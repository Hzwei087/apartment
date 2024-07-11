package com.atguigu.lease.web.admin.schedule;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ScheduledTasks {
    @Autowired
    private LeaseAgreementService leaseAgreementService;

    @Scheduled(cron = "0 0 0 * * *")
    public void checkLeaseStatus(){
        LambdaUpdateWrapper<LeaseAgreement> leaseAgreementUpdateWrapper = new LambdaUpdateWrapper<>();
        leaseAgreementUpdateWrapper.in(LeaseAgreement::getStatus,LeaseStatus.SIGNED,LeaseStatus.WITHDRAWING);
        leaseAgreementUpdateWrapper.le(LeaseAgreement::getLeaseEndDate,new Date());
        leaseAgreementUpdateWrapper.set(LeaseAgreement::getStatus, LeaseStatus.EXPIRED);
        leaseAgreementService.update(leaseAgreementUpdateWrapper);


    }

}
