package com.example.intermediate.repository;

import com.example.intermediate.domain.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {

}
