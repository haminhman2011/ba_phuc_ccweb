package com.dangkymonhoc.exception;

/** Vi phạm quy định nghiệp vụ; message được hiển thị nguyên văn cho người dùng. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
