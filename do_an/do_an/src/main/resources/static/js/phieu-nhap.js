// Xử lý thêm / xoá dòng hàng ở màn hình Nhập kho (UC3).
//
// Để ở file riêng chứ không viết thẳng trong HTML, vì đoạn code này có regex
// chứa hai dấu ngoặc vuông liền nhau. Thymeleaf hiểu ký hiệu đó là cú pháp nhúng
// biến nên sẽ cố diễn giải rồi báo lỗi, làm trang bị cắt cụt ngay giữa chừng.
// File .js nằm trong static/ thì Thymeleaf không đọc tới, nên không còn vấn đề.

function thanBang() {
    return document.querySelector('#bangChiTiet tbody');
}

function themDong() {
    var mau = document.getElementById('mauDong');
    thanBang().appendChild(mau.content.cloneNode(true));
    danhSoLai();
}

function xoaDong(nut) {
    nut.closest('tr').remove();
    danhSoLai();
}

// Spring gom dữ liệu theo chiTiet[0], chiTiet[1]... nên số thứ tự phải liền mạch.
// Xoá một dòng ở giữa là thủng số, vì vậy đánh số lại toàn bộ sau mỗi lần thêm/xoá.
function danhSoLai() {
    var mauChiSo = /chiTiet\[[^\]]*\]/;
    var dsDong = thanBang().querySelectorAll('tr');

    dsDong.forEach(function (tr, i) {
        tr.querySelectorAll('input, select').forEach(function (o) {
            if (o.name) {
                o.name = o.name.replace(mauChiSo, 'chiTiet[' + i + ']');
                o.id = o.name.replace(/[\[\]]/g, '');
            }
        });
        var stt = tr.querySelector('.stt');
        if (stt) {
            stt.textContent = i + 1;
        }
    });

    var bangRong = document.getElementById('bangRong');
    if (bangRong) {
        bangRong.style.display = dsDong.length === 0 ? '' : 'none';
    }
}

document.addEventListener('DOMContentLoaded', danhSoLai);
