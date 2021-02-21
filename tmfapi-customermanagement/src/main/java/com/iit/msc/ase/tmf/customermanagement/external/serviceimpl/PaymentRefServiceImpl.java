package com.iit.msc.ase.tmf.customermanagement.external.serviceimpl;

import com.iit.msc.ase.tmf.customermanagement.domain.boundary.repository.PaymentRefRepository;
import com.iit.msc.ase.tmf.customermanagement.domain.boundary.service.PaymentRefService;
import com.iit.msc.ase.tmf.customermanagement.domain.model.PaymentRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Dhanushka Sampath
 * @version 1.0
 * @since 2021-02-21
 */
@Service
public class PaymentRefServiceImpl implements PaymentRefService {

    @Autowired
    private PaymentRefRepository paymentRefRepository;

    @Override
    public PaymentRef create(PaymentRef paymentRef) {
        log("create method of PaymentRef started");
        return paymentRefRepository.save(paymentRef);
    }

}
