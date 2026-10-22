package com.dangkymonhoc.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.dangkymonhoc.config.AppProperties;
import com.dangkymonhoc.repository.PhanCongRepository;

import lombok.RequiredArgsConstructor;

/** Tiện ích về học kỳ: kiểm tra mẫu HK{1|2|3}-{năm} và sắp xếp mới nhất lên trước. */
@Service
@RequiredArgsConstructor
public class HocKyService {

    private static final Pattern MAU_HOC_KY = Pattern.compile("^HK([123])-(\\d{4})$");

    /** HK1-2026 > HK3-2025 > HK2-2025 > HK1-2025; chuỗi sai mẫu xếp cuối. */
    private static final Comparator<String> MOI_NHAT_TRUOC = Comparator
            .comparingInt(HocKyService::thuTu).reversed()
            .thenComparing(Comparator.naturalOrder());

    private final PhanCongRepository phanCongRepository;
    private final AppProperties appProperties;

    public String hienTai() {
        return appProperties.getHocKyHienTai();
    }

    public boolean hopLe(String hocKy) {
        return hocKy != null && MAU_HOC_KY.matcher(hocKy).matches();
    }

    /** Trả về hocKy nếu hợp lệ, ngược lại trả về học kỳ hiện tại. */
    public String chuanHoa(String hocKy) {
        String hk = hocKy == null ? "" : hocKy.trim().toUpperCase();
        return hopLe(hk) ? hk : hienTai();
    }

    /** Danh sách học kỳ đã có phân công cộng thêm học kỳ hiện tại và các học kỳ truyền vào. */
    public List<String> tatCa(String... them) {
        TreeSet<String> ds = new TreeSet<>(MOI_NHAT_TRUOC);
        ds.addAll(phanCongRepository.findAllHocKy());
        ds.add(hienTai());
        for (String hk : them) {
            if (hopLe(hk)) {
                ds.add(hk);
            }
        }
        return List.copyOf(ds);
    }

    /** Sắp xếp một tập học kỳ bất kỳ, luôn có học kỳ hiện tại. */
    public List<String> sapXep(Collection<String> hocKys) {
        TreeSet<String> ds = new TreeSet<>(MOI_NHAT_TRUOC);
        ds.addAll(hocKys);
        ds.add(hienTai());
        return List.copyOf(ds);
    }

    public static Comparator<String> moiNhatTruoc() {
        return MOI_NHAT_TRUOC;
    }

    private static int thuTu(String hocKy) {
        Matcher m = MAU_HOC_KY.matcher(hocKy == null ? "" : hocKy);
        if (!m.matches()) {
            return Integer.MIN_VALUE;
        }
        return Integer.parseInt(m.group(2)) * 10 + Integer.parseInt(m.group(1));
    }
}
