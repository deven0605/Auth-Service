package com.thalicloud.auth.seeder;

import com.thalicloud.auth.entity.Kitchen;
import com.thalicloud.auth.entity.KitchenAddress;
import com.thalicloud.auth.entity.Vendor;
import com.thalicloud.auth.enums.VendorRole;
import com.thalicloud.auth.repository.KitchenAddressRepository;
import com.thalicloud.auth.repository.KitchenRepository;
import com.thalicloud.auth.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * Dev-only seed data for the Home/Kitchen-discovery feature (SRS M3) — inserts a
 * handful of demo kitchens near Saket, Delhi so the customer app has something
 * real to browse without a manual vendor signup. Runs once: skipped entirely if
 * any vendor already exists (either from a real signup or a previous seed run).
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final VendorRepository vendorRepository;
    private final KitchenRepository kitchenRepository;
    private final KitchenAddressRepository kitchenAddressRepository;
    private final PasswordEncoder passwordEncoder;

    private record Sample(
            String kitchenName, String ownerName, String email, String mobile,
            LocalTime opensAt, LocalTime closesAt, boolean veg, int etaMin, int etaMax, String imageUrl,
            double lat, double lng, String street, String pincode
    ) {}

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (vendorRepository.count() > 0) {
            log.info("[DataSeeder] Vendors already present — skipping demo kitchen seed.");
            return;
        }

        List<Sample> samples = sampleKitchens();
        for (Sample s : samples) {
            Vendor vendor = Vendor.builder()
                    .ownerName(s.ownerName())
                    .email(s.email())
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .ownerMobile(s.mobile())
                    .role(VendorRole.VENDOR)
                    .active(true)
                    .verified(true)
                    .build();
            vendorRepository.save(vendor);

            Kitchen kitchen = Kitchen.builder()
                    .vendor(vendor)
                    .kitchenName(s.kitchenName())
                    .contactNumber(s.mobile())
                    .opensAt(s.opensAt())
                    .closesAt(s.closesAt())
                    .imageUrl(s.imageUrl())
                    .veg(s.veg())
                    .etaMinMinutes(s.etaMin())
                    .etaMaxMinutes(s.etaMax())
                    .build();
            kitchenRepository.save(kitchen);

            KitchenAddress address = KitchenAddress.builder()
                    .kitchen(kitchen)
                    .streetAddress(s.street())
                    .city("New Delhi")
                    .state("Delhi")
                    .pincode(s.pincode())
                    .latitude(BigDecimal.valueOf(s.lat()))
                    .longitude(BigDecimal.valueOf(s.lng()))
                    .build();
            kitchenAddressRepository.save(address);
        }

        log.info("[DataSeeder] Seeded {} demo kitchens near Saket, Delhi.", samples.size());
    }

    // All within ~4km of Saket (28.5245, 77.2066) — the default 5km discovery radius.
    // "Maa Ki Rasoi" has lunch-only hours so it demonstrates the CLOSED badge (FR-3.3)
    // outside that window.
    private List<Sample> sampleKitchens() {
        return List.of(
                new Sample("Sharma's Kitchen", "Rekha Sharma", "rekha.sharma@ghar.dev", "9810000001",
                        LocalTime.of(8, 0), LocalTime.of(22, 0), true, 25, 35,
                        "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=600&q=80",
                        18.6323, 73.7365, "12 Saket Market", "110017"),
                new Sample("Desi Dhaba", "Harpreet Singh", "harpreet.singh@ghar.dev", "9810000002",
                        LocalTime.of(8, 0), LocalTime.of(22, 0), false, 30, 45,
                        "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=600&q=80",
                        18.6323, 73.7365, "8 Press Enclave Road", "110017"),
                new Sample("Maa Ki Rasoi", "Kavita Verma", "kavita.verma@ghar.dev", "9810000003",
                        LocalTime.of(11, 0), LocalTime.of(15, 0), false, 20, 30,
                        "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=600&q=80",
                        18.6323, 73.7365, "22 Malviya Nagar", "110017"),
                new Sample("Green Leaf Tiffins", "Anjali Nair", "anjali.nair@ghar.dev", "9810000004",
                        LocalTime.of(7, 0), LocalTime.of(21, 0), true, 20, 30,
                        "https://images.unsplash.com/photo-1547592180-85f173990554?w=600&q=80",
                        18.6323, 73.7365, "3 Press Enclave Marg", "110017"),
                new Sample("Punjabi Rasoi", "Gurmeet Kaur", "gurmeet.kaur@ghar.dev", "9810000005",
                        LocalTime.of(9, 0), LocalTime.of(23, 0), false, 35, 50,
                        "https://images.unsplash.com/photo-1567188040759-fb8a883dc6d8?w=600&q=80",
                        18.6323, 73.7365, "45 Chirag Delhi", "110017")
        );
    }
}
