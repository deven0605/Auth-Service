package com.thalicloud.auth.seeder;

import com.thalicloud.auth.entity.DeliveryPartner;
import com.thalicloud.auth.enums.PartnerLifecycleState;
import com.thalicloud.auth.repository.DeliveryPartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dev-only seed data for delivery partner auth (SRS M1) — one partner per
 * lifecycleState so FR-1.10 routing can be exercised end-to-end with the
 * hardcoded OTP "1234" without going through the M2 registration flow.
 * Skipped entirely if a delivery partner already exists.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DeliveryPartnerSeeder implements ApplicationRunner {

    private final DeliveryPartnerRepository deliveryPartnerRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (deliveryPartnerRepository.count() > 0) {
            log.info("[DeliveryPartnerSeeder] Delivery partners already present — skipping seed.");
            return;
        }

        deliveryPartnerRepository.save(DeliveryPartner.builder()
                .phone("+919820000001")
                .name("Rahul Pending")
                .lifecycleState(PartnerLifecycleState.PENDING_VERIFICATION)
                .registrationComplete(true)
                .build());

        deliveryPartnerRepository.save(DeliveryPartner.builder()
                .phone("+919820000002")
                .name("Suresh Approved")
                .lifecycleState(PartnerLifecycleState.APPROVED)
                .registrationComplete(true)
                .build());

        deliveryPartnerRepository.save(DeliveryPartner.builder()
                .phone("+919820000003")
                .name("Vikas Suspended")
                .lifecycleState(PartnerLifecycleState.SUSPENDED)
                .registrationComplete(true)
                .build());

        log.info("[DeliveryPartnerSeeder] Seeded 3 demo delivery partners " +
                "(+919820000001 PENDING_VERIFICATION, +919820000002 APPROVED, +919820000003 SUSPENDED). " +
                "Any other 10-digit number is treated as a new partner. OTP is always 1234.");
    }
}
