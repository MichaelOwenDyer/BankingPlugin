package com.monst.bankingplugin.persistence.repository;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.function.Supplier;

public abstract class EntityRepository<Entity> {

    private static class ClosingJPAQuery<Entity> extends JPAQuery<Entity> {
        public ClosingJPAQuery(EntityManager em) {
            super(em);
        }
        @Override
        protected void reset() {
            entityManager.close();
        }
    }

    final Supplier<EntityManager> emf;
    final EntityPath<Entity> entity;

    public EntityRepository(Supplier<EntityManager> emf, EntityPath<Entity> entity) {
        this.emf = emf;
        this.entity = entity;
    }

    <T> JPAQuery<T> select(Expression<T> expression) {
        return new ClosingJPAQuery<>(emf.get()).select(expression).from(entity);
    }

    JPAQuery<Entity> entities() {
        return select(entity);
    }

    public List<Entity> findAll() {
        return entities().fetch();
    }

}
