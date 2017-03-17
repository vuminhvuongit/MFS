package com.csc.mfs.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.csc.mfs.model.Files;
import com.csc.mfs.model.User;

/**
 * This interface provide method interactive db(files table)
 * @author VuMin
 *
 */
public interface FilesRepository extends JpaRepository<Files, Integer> {
	
	/**
	 * This method is used to search file by info file(name, type, categories, size) 
	 * @param fileInfo
	 * @param number
	 * @param pageSize
	 * @return List<Files>
	 */
	@Query(value="SELECT * FROM files WHERE active=1 AND (name like %:fileInfo%"
			+ " OR size like %:fileInfo% OR"
			+ " user_id IN (SELECT id FROM user WHERE name=:fileInfo)"
			+ " OR id_type IN (SELECT id FROM categories_type WHERE file_type LIKE %:fileInfo%)"
            + " OR id_type IN (SELECT id FROM categories_type t WHERE t.category_id "
            + "	IN ( SELECT id FROM categories WHERE name LIKE %:fileInfo%)"
            + ")) LIMIT :number, :pageSize" , nativeQuery=true)
	List<Files> findByInfo(@Param("fileInfo") String fileInfo, @Param("number") int number, @Param("pageSize") int pageSize);
	
	@Query(value="SELECT COUNT(*) FROM files WHERE active=1 AND (name like %:fileInfo%"
			+ " OR size like %:fileInfo% OR"
			+ " user_id IN (SELECT id FROM user WHERE name=:fileInfo)"
			+ " OR id_type IN (SELECT id FROM categories_type WHERE file_type LIKE %:fileInfo%)"
            + " OR id_type IN (SELECT id FROM categories_type t WHERE t.category_id "
            + "	IN ( SELECT id FROM categories WHERE name LIKE %:fileInfo%)"
            + "))" , nativeQuery=true)
	long countSearch(@Param("fileInfo") String fileInfo);
	
	/**
	 * Get total size file which user uploaded in day
	 * @param idUser
	 * @param dateUpload
	 * @return Object
	 */
	@Query(value="SELECT SUM(size) FROM files WHERE active=1 AND user_id=:idUser and "
			+ "(UNIX_TIMESTAMP(dateupload)*1000)=:dateUpload" , nativeQuery=true)
	Object sumSizeUploadInDay(@Param("idUser") int idUser, @Param("dateUpload") Long dateUpload);
	
	/**
	 * get total size file which user uploaded(all)
	 * @param idUser
	 * @return Object
	 */
	@Query(value="SELECT SUM(size) FROM files WHERE active=1 AND user_id=:idUser", nativeQuery=true)
	Object sumSizeUpload(@Param("idUser") int idUser);
	
	/**
	 * Remove all record of user(in table file)
	 * @param user
	 */
	@Transactional
	void removeByUserId(User user);
	 
	/**
	 * Update user_id = 0 when user is deleted(foreign key)
	 * @param idUser
	 */
	@Transactional
	@Modifying
	@Query(value="UPDATE files SET user_id= 0 WHERE user_id=:idUser", nativeQuery=true)
	void UpdateUser(@Param("idUser") int idUser);

	/**
	 * Get all file of user
	 * @param user
	 * @return List<Files>
	 */
	List<Files> findByUserId(int user);
	
	List<Files> findByName(@Param("name") String name);
	/**
	 * Get all file of user(pagination)
	 * @param idUser
	 * @param number
	 * @param pageSize
	 * @return List<Files>
	 */
	@Query(value="SELECT * FROM files WHERE active=1 AND user_id=:idUser"
			+ " LIMIT :number, :pageSize", nativeQuery=true)
	List<Files> findFileActiveByUserId(@Param("idUser") int idUser, 
			@Param("number") int number, @Param("pageSize") int pageSize);
	
	/**
	 * Get all file of user
	 * @param idUser
	 * @return long
	 */
	@Query(value="SELECT count(*) FROM files WHERE active=1 AND user_id=:idUser", nativeQuery=true)
	long countFileActiveByUserId(@Param("idUser") int idUser);
	
	
	/**
	 * Count all file
	 * @return long
	 */
	@Query(value="SELECT count(*) FROM files WHERE active=1", nativeQuery=true)
	long countFile();
	
	/**
	 * Update active when user delete file
	 * @param idFile
	 */
	@Transactional
	@Modifying
	@Query(value="UPDATE files SET active=0 WHERE id=:idFile", nativeQuery=true)
	void updateActive(@Param("idFile") int idFile);
	
	/**
	 * get files has most downloaded
	 * @return List<Files>
	 */
	@Query(value="SELECT f.* FROM `download` d, files f WHERE f.id = d.id_file "
			+ " GROUP BY id_file ORDER BY COUNT(*) DESC" , nativeQuery=true)
	List<Files> getBestDownload();
	
	/**
	 * get all file(pagination)
	 * @param idUser
	 * @param number
	 * @param pageSize
	 * @return
	 */
	@Query(value="SELECT f.*, u.name as uploader"
			+ " FROM files f, user u WHERE f.active=1 AND u.id=f.user_id "
			+ " LIMIT :number, :pageSize", nativeQuery=true)
	List<Object> getAllFilePagination( @Param("number") int number, 
			@Param("pageSize") int pageSize);
	
	/**
	 * 
	 */
	//@Query(value="SELECT * FROM files WHERE active=1 ORDER BY ?#",nativeQuery=true)
	Page<Files> findByActiveAndSharing(int active, int sharing, Pageable pagable);
	
	//SELECT f.*, u.last_name FROM categories c INNER JOIN categories_type ct ON ct.category_id = c.id 
	//INNER JOIN files f ON f.id_type = ct.id INNER JOIN user u ON u.id=f.user_id WHERE c.id=1
	@Query(value="SELECT f.*, u.last_name FROM "
			+ " categories INNER JOIN categories_type ON categories_type.category_id = categories.id "
				+" INNER JOIN files f ON f.id_type = categories_type.id "
				+ " INNER JOIN user u ON u.id=f.user_id "
				+ " WHERE (categories.name=:nameCategory OR categories.name like :nameCategory) AND f.active =1 ORDER BY ?#{#pageable}"
				, countQuery = "SELECT count(*) FROM "
				+" categories INNER JOIN categories_type ON categories_type.category_id = categories.id" 
				+" INNER JOIN files ON files.id_type = categories_type.id" 
				+" INNER JOIN user ON user.id=files.user_id" 
				+" WHERE (categories.name=:nameCategory OR categories.name like :nameCategory) AND files.active =1"
				,nativeQuery=true)
	Page<Object> getFileByCategory(@Param("nameCategory") String nameCategory, Pageable pageable);

	
}


/*
 * @Query(value="SELECT f.*, u.last_name FROM categories c INNER JOIN categories_type ct ON ct.category_id = c.id "
				+" INNER JOIN files f ON f.id_type = ct.id "
				+ " INNER JOIN user u ON u.id=f.user_id "
				+ " WHERE c.name=:nameCategory AND f.active =1 LIMIT :number, :pageSize",nativeQuery=true)
	List<Object> getFileByCategory(@Param("nameCategory") String nameCategory, @Param("number") int number, @Param("pageSize") int pageSize);
 */




