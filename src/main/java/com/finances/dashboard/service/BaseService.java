package com.finances.dashboard.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finances.dashboard.model.DomainEntity;

import jakarta.transaction.Transactional;

public abstract class BaseService <T extends DomainEntity> {
    protected abstract JpaRepository<T, Long> getRepository();

    public List<T> findAll() {
        return getRepository().findAll();
    }

    public List<T> findAllActive() {
        return getRepository().findAll().stream()
                .filter(entity -> entity.getDeletedAt() == null)
                .toList();
    }

    public T save(T entity) {
        return getRepository().save(entity);
    }

    public T findById(Long id) {
        return getRepository().findById(id)
                .orElseThrow(() -> new RuntimeException("Entity not found with id: " + id));
    }

    @Transactional
    public void softDelete(T entity) {
        T entityToDelete = getRepository().findById(entity.getId())
                .orElseThrow(() -> new RuntimeException("Entity not found with id: " + entity.getId()));
        entityToDelete.softDelete();
    }

}
