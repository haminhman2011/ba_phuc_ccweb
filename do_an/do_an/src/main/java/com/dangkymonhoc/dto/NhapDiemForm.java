package com.dangkymonhoc.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

/** Form nhập điểm cả lớp: mỗi phần tử là (mã SV, chuỗi điểm). */
@Data
public class NhapDiemForm {

    private List<Item> items = new ArrayList<>();

    @Data
    public static class Item {
        private String maSV;
        private String diem;
    }

    /** Chuyển sang map maSV -> chuỗi điểm, giữ thứ tự gửi lên. */
    public Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (Item item : items) {
            if (item.getMaSV() != null) {
                map.put(item.getMaSV(), item.getDiem());
            }
        }
        return map;
    }
}
