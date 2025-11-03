package com.tamv.systema.repositories;

import com.tamv.systema.entities.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    public List<InvoiceItem> findByInvoiceId(Long invoiceId);
}
