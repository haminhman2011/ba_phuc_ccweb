package com.manha.lab0910.form;

import com.manha.lab0910.entity.NguoiDung;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * (UC8) Dữ liệu form thêm/sửa tài khoản.
 *
 * Không bind thẳng vào entity NguoiDung vì ô mật khẩu ở đây là mật khẩu THÔ
 * người dùng vừa gõ, còn cột mat_khau trong DB là chuỗi BCrypt.
 * Ngoài ra khi sửa, để trống ô mật khẩu nghĩa là "giữ nguyên mật khẩu cũ".
 */
public class NguoiDungForm {

    private Long id;

    @NotBlank(message = "Vui lòng nhập tên đăng nhập")
    @Size(max = 50, message = "Tên đăng nhập tối đa 50 ký tự")
    @Pattern(regexp = "^\\S*$", message = "Tên đăng nhập không được có dấu cách")
    private String tenDangNhap;

    // Bắt buộc khi tạo mới, để trống khi sửa thì giữ mật khẩu cũ.
    // Việc kiểm tra bắt buộc nằm ở controller vì còn tuỳ thêm mới hay sửa.
    private String matKhau;

    @NotBlank(message = "Vui lòng nhập họ tên")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @NotNull(message = "Vui lòng chọn vai trò")
    private NguoiDung.VaiTro vaiTro;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private Integer trangThai = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public NguoiDung.VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(NguoiDung.VaiTro vaiTro) { this.vaiTro = vaiTro; }
    public Integer getTrangThai() { return trangThai; }
    public void setTrangThai(Integer trangThai) { this.trangThai = trangThai; }
}
