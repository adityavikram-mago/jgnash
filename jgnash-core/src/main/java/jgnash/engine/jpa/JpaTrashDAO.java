/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2013 Craig Cavanaugh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jgnash.engine.jpa;


import jgnash.engine.StoredObject;
import jgnash.engine.TrashObject;
import jgnash.engine.dao.TrashDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Trash DAO
 *
 * @author Craig Cavanaugh
 */
class JpaTrashDAO extends AbstractJpaDAO implements TrashDAO {

    private static final Logger logger = Logger.getLogger(JpaTrashDAO.class.getName());

    JpaTrashDAO(final EntityManager entityManager, final boolean isRemote) {
        super(entityManager, isRemote);
    }

    @Override
    public List<TrashObject> getTrashObjects() {

        try {
            emLock.lock();

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<TrashObject> cq = cb.createQuery(TrashObject.class);
            Root<TrashObject> root = cq.from(TrashObject.class);
            cq.select(root);

            TypedQuery<TrashObject> q = em.createQuery(cq);

            return new ArrayList<>(q.getResultList());
        } finally {
            emLock.unlock();
        }
    }

    @Override
    public void add(final TrashObject trashObject) {
        try {
            emLock.lock();
            em.getTransaction().begin();

            em.merge(trashObject.getObject());
            em.persist(trashObject);

            em.getTransaction().commit();
        } finally {
            emLock.unlock();
        }
    }

    @Override
    public void remove(final TrashObject trashObject) {
        try {
            emLock.lock();

            em.getTransaction().begin();

            StoredObject object = trashObject.getObject();

            em.remove(object);
            em.remove(trashObject);

            em.getTransaction().commit();

            logger.info("Removed TrashObject");
        } finally {
            emLock.unlock();
        }
    }
}
