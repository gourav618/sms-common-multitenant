package org.sms.io.common.repo;


import org.sms.io.common.model.MasterModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterRepo extends JpaRepository<MasterModel, String> {
}
