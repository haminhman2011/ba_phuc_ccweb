/* Hệ thống đăng ký môn học – hành vi giao diện dùng chung */
(function () {
    'use strict';

    const ctxMeta = document.querySelector('meta[name="ctx"]');
    const CTX = (ctxMeta ? ctxMeta.content : '/').replace(/\/$/, '');

    /** Bỏ dấu tiếng Việt để tìm kiếm không phân biệt dấu. */
    function normalize(text) {
        return (text || '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd');
    }

    /** "paramMaMH" → "maMH" */
    function lowerFirst(text) {
        return text.charAt(0).toLowerCase() + text.slice(1);
    }

    // Thông báo flash tự ẩn sau 4 giây
    function initAlerts() {
        document.querySelectorAll('.alert[data-auto-dismiss]').forEach(function (el) {
            setTimeout(function () {
                bootstrap.Alert.getOrCreateInstance(el).close();
            }, 4000);
        });
    }

    function initTooltips() {
        document.querySelectorAll('[data-tooltip]').forEach(function (el) {
            new bootstrap.Tooltip(el, { trigger: 'hover focus' });
        });
    }

    // Modal xác nhận chung: nút có data-confirm-action mở modal và gửi POST (có CSRF)
    function initConfirm() {
        const modalEl = document.getElementById('confirmModal');
        if (!modalEl) {
            return;
        }
        const form = document.getElementById('confirmForm');
        const params = document.getElementById('confirmParams');
        const submit = document.getElementById('confirmButton');
        const icon = document.getElementById('confirmIcon');

        document.addEventListener('click', function (e) {
            const btn = e.target.closest('[data-confirm-action]');
            if (!btn || btn.disabled) {
                return;
            }
            e.preventDefault();
            const d = btn.dataset;
            form.action = d.confirmAction;
            document.getElementById('confirmTitle').textContent = d.confirmTitle || 'Xác nhận xóa';
            document.getElementById('confirmMessage').textContent = d.confirmMessage || 'Bạn có chắc muốn xóa';
            document.getElementById('confirmTarget').textContent = d.confirmTarget || '';

            params.innerHTML = '';
            Object.keys(d).filter(function (k) { return k.indexOf('param') === 0 && k.length > 5; })
                .forEach(function (k) {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = lowerFirst(k.substring(5));
                    input.value = d[k];
                    params.appendChild(input);
                });

            const variant = d.confirmVariant || 'danger';
            submit.textContent = d.confirmButton || 'Xóa';
            submit.className = 'btn btn-' + variant;
            icon.className = 'confirm-icon' + (variant === 'danger' ? '' : ' ' + variant);
            bootstrap.Modal.getOrCreateInstance(modalEl).show();
        });
    }

    // Modal thêm/sửa danh mục (Khoa, Lớp, Môn học)
    function initFormModals() {
        document.querySelectorAll('.modal[data-form-modal]').forEach(function (modal) {
            const form = modal.querySelector('form');

            modal.addEventListener('show.bs.modal', function (e) {
                const trigger = e.relatedTarget;
                if (!trigger) {
                    return; // mở tự động sau khi server báo lỗi: giữ nguyên dữ liệu và lỗi
                }
                const edit = trigger.dataset.mode === 'edit';
                form.querySelectorAll('.is-invalid').forEach(function (el) { el.classList.remove('is-invalid'); });
                modal.querySelectorAll('.modal-error').forEach(function (el) { el.remove(); });

                form.querySelectorAll('input:not([type=hidden]):not([type=checkbox]):not([type=radio]), select, textarea')
                    .forEach(function (el) {
                        if (!el.name) {
                            return;
                        }
                        if (edit && trigger.dataset[el.name] !== undefined) {
                            el.value = trigger.dataset[el.name];
                        } else {
                            el.value = el.dataset.default !== undefined ? el.dataset.default : '';
                        }
                    });

                const editInput = form.querySelector('input[name="editMode"]');
                if (editInput) {
                    editInput.value = edit ? 'true' : 'false';
                }
                form.querySelectorAll('[data-pk]').forEach(function (el) { el.readOnly = edit; });
                const title = modal.querySelector('.modal-title');
                if (title) {
                    title.textContent = edit ? modal.dataset.titleEdit : modal.dataset.titleAdd;
                }
            });

            modal.addEventListener('shown.bs.modal', function () {
                const first = form.querySelector('input:not([type=hidden]):not([readonly]), select');
                if (first) {
                    first.focus();
                }
            });

            if (modal.dataset.openOnLoad === 'true') {
                bootstrap.Modal.getOrCreateInstance(modal).show();
            }
        });
    }

    // Select đổi giá trị thì tự submit form chứa nó
    function initAutoSubmit() {
        document.querySelectorAll('[data-auto-submit]').forEach(function (el) {
            el.addEventListener('change', function () { el.form.submit(); });
        });
    }

    // Lọc nhanh các dòng của bảng ở phía trình duyệt
    function initTableFilter() {
        document.querySelectorAll('input[data-filter-table]').forEach(function (input) {
            const table = document.querySelector(input.dataset.filterTable);
            const empty = input.dataset.filterEmpty ? document.querySelector(input.dataset.filterEmpty) : null;
            if (!table) {
                return;
            }
            input.addEventListener('input', function () {
                const q = normalize(input.value.trim());
                let visible = 0;
                table.querySelectorAll('tbody tr').forEach(function (tr) {
                    const match = normalize(tr.textContent).indexOf(q) !== -1;
                    tr.classList.toggle('d-none', !match);
                    if (match) {
                        visible++;
                    }
                });
                if (empty) {
                    empty.classList.toggle('d-none', visible > 0);
                }
            });
        });
    }

    // Nút con mắt ẩn/hiện mật khẩu
    function initPasswordToggle() {
        document.querySelectorAll('[data-toggle-password]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                const input = document.querySelector(btn.dataset.togglePassword);
                const show = input.type === 'password';
                input.type = show ? 'text' : 'password';
                btn.querySelector('i').className = show ? 'bi bi-eye-slash' : 'bi bi-eye';
                btn.setAttribute('aria-label', show ? 'Ẩn mật khẩu' : 'Hiện mật khẩu');
            });
        });
    }

    // Ô mã tự chuyển sang chữ in hoa
    function initUppercase() {
        document.querySelectorAll('input[data-uppercase]').forEach(function (input) {
            input.addEventListener('input', function () {
                const pos = input.selectionStart;
                input.value = input.value.toUpperCase();
                input.setSelectionRange(pos, pos);
            });
        });
    }

    // Select lớp nạp lại theo khoa bằng fetch GET /api/lop?maKhoa=
    function initLopSelect() {
        document.querySelectorAll('select[data-lop-target]').forEach(function (khoa) {
            const lop = document.querySelector(khoa.dataset.lopTarget);
            if (!lop) {
                return;
            }
            khoa.addEventListener('change', function () {
                lop.innerHTML = '';
                lop.add(new Option(lop.dataset.placeholder || '-- Chọn lớp --', ''));
                const done = function () {
                    if (khoa.hasAttribute('data-submit-after')) {
                        khoa.form.submit();
                    }
                };
                if (!khoa.value) {
                    done();
                    return;
                }
                fetch(CTX + '/api/lop?maKhoa=' + encodeURIComponent(khoa.value), { headers: { Accept: 'application/json' } })
                    .then(function (res) { return res.ok ? res.json() : []; })
                    .then(function (ds) {
                        ds.forEach(function (l) { lop.add(new Option(l.maLop + ' – ' + l.tenLop, l.maLop)); });
                    })
                    .catch(function (err) { console.error('Không tải được danh sách lớp', err); })
                    .finally(done);
            });
        });
    }

    // Lọc option của một select theo giá trị select khác (giảng viên theo khoa)
    function initOptionFilter() {
        document.querySelectorAll('select[data-filter-target]').forEach(function (src) {
            const target = document.querySelector(src.dataset.filterTarget);
            if (!target) {
                return;
            }
            const apply = function () {
                Array.prototype.forEach.call(target.options, function (o) {
                    if (!o.value) {
                        return;
                    }
                    const show = !src.value || o.dataset.khoa === src.value;
                    o.hidden = !show;
                    o.disabled = !show;
                });
                const selected = target.options[target.selectedIndex];
                if (selected && selected.disabled) {
                    target.value = '';
                }
            };
            src.addEventListener('change', apply);
            apply();
        });
    }

    // Form thêm tài khoản: hiện select "Gắn với sinh viên/giảng viên" theo quyền
    function initRoleToggle() {
        const radios = document.querySelectorAll('input[data-role-radio]');
        if (!radios.length) {
            return;
        }
        const update = function () {
            const checked = document.querySelector('input[data-role-radio]:checked');
            const role = checked ? checked.value : '';
            document.querySelectorAll('[data-role-show]').forEach(function (group) {
                const show = group.dataset.roleShow === role;
                group.classList.toggle('d-none', !show);
                group.querySelectorAll('select').forEach(function (s) { s.disabled = !show; });
            });
        };
        radios.forEach(function (r) { r.addEventListener('change', update); });
        update();
    }

    // Nhập điểm: kiểm tra 0–10 khi gõ, đếm số đã nhập, cảnh báo khi rời trang chưa lưu
    function initDiemForm() {
        const form = document.querySelector('form[data-diem-form]');
        if (!form) {
            return;
        }
        const inputs = form.querySelectorAll('.diem-input');
        const warning = document.getElementById('unsavedWarning');
        const counter = document.getElementById('daNhapCount');
        let dirty = form.dataset.dirty === 'true';
        let submitting = false;

        const hopLe = function (input) {
            if (input.validity && input.validity.badInput) {
                return false;
            }
            const v = input.value.trim();
            if (v === '') {
                return true;
            }
            const n = Number(v.replace(',', '.'));
            return !isNaN(n) && n >= 0 && n <= 10;
        };
        const refresh = function () {
            let count = 0;
            inputs.forEach(function (i) {
                if (i.value.trim() !== '') {
                    count++;
                }
            });
            if (counter) {
                counter.textContent = count;
            }
            if (warning) {
                warning.classList.toggle('d-none', !dirty);
            }
        };

        inputs.forEach(function (input) {
            input.dataset.original = input.value;
            input.addEventListener('input', function () {
                input.classList.toggle('is-invalid', !hopLe(input));
                dirty = Array.prototype.some.call(inputs, function (x) { return x.value !== x.dataset.original; })
                    || form.dataset.dirty === 'true';
                refresh();
            });
        });

        form.addEventListener('submit', function () { submitting = true; });
        window.addEventListener('beforeunload', function (e) {
            if (dirty && !submitting) {
                e.preventDefault();
                e.returnValue = '';
            }
        });
        refresh();
    }

    function initPrint() {
        document.querySelectorAll('[data-print]').forEach(function (btn) {
            btn.addEventListener('click', function () { window.print(); });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        initAlerts();
        initTooltips();
        initConfirm();
        initFormModals();
        initAutoSubmit();
        initTableFilter();
        initPasswordToggle();
        initUppercase();
        initLopSelect();
        initOptionFilter();
        initRoleToggle();
        initDiemForm();
        initPrint();
    });
})();
