package com.inventory.repository;

import com.inventory.model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    
    @Query("SELECT new com.inventory.model.User(u.username, u.password, u.role) FROM User u WHERE u.username = ?1")
    Optional<User> findByUsernameWithoutPicture(String username);

    @Query("SELECT new com.inventory.model.User(u.username, u.password, u.role) FROM User u WHERE UPPER(u.username) = UPPER(?1)")
    Optional<User> findByUsernameWithoutPictureIgnoreCase(String username);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.profilePicture = ?1 WHERE u.username = ?2")
    void updateProfilePictureByUsername(byte[] profilePicture, String username);
    
    @Query("SELECT u.profilePicture FROM User u WHERE u.username = ?1")
    Optional<byte[]> findProfilePictureByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE UPPER(u.username) = UPPER(?1)")
    Optional<User> findByUsernameIgnoreCase(String username);
}
