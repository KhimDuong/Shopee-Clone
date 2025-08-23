package com.shopeeclone.shopee_api.repository;

import com.shopeeclone.shopee_api.model.ClientCryptoMeta;
import com.shopeeclone.shopee_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientCryptoMetaRepository extends JpaRepository<ClientCryptoMeta, String> {

    // Find crypto metadata by user
    Optional<ClientCryptoMeta> findByUser(User user);

    // Or find by userId directly
    Optional<ClientCryptoMeta> findByUserId(Long userId);
}
