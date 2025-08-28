package ambient_intelligence.dal;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import ambient_intelligence.data.ObjectEntity;

public interface ObjectCrud extends MongoRepository<ObjectEntity, String> {

	public Optional<ObjectEntity> findByIdAndActiveTrue(@Param("id") String id);
	
	public List<ObjectEntity> findAllById(@Param("id") String id, Pageable pageable);
	
	public List<ObjectEntity> findAllByIdAndActiveTrue(@Param("id") String id, Pageable pageable);
	
	public List<ObjectEntity> findAllByActiveTrue(Pageable pageable);
	
	// find all objects that have the passed ObjectEntity parent
	public List<ObjectEntity> findAllByParent_Id(@Param("parent") String parentId, Pageable pageable);
	
	public List<ObjectEntity> findAllByParent_IdAndActiveTrue(@Param("parent") String parentId, Pageable pageable);

	public List<ObjectEntity> findByAlias(@Param("alias") String alias, Pageable pageable);

	public List<ObjectEntity> findByAliasAndActiveTrue(@Param("alias") String alias, Pageable pageable);

	public List<ObjectEntity> findByAliasLike(@Param("pattern") String pattern, Pageable pageable);

	public List<ObjectEntity> findByAliasLikeAndActiveTrue(@Param("pattern") String pattern, Pageable pageable);

	public List<ObjectEntity> findByType(@Param("type") String type, Pageable pageable);

	public List<ObjectEntity> findByTypeAndActiveTrue(@Param("type") String type, Pageable pageable);

	public List<ObjectEntity> findByStatus(@Param("status") String status, Pageable pageable);

	public List<ObjectEntity> findByStatusAndActiveTrue(@Param("status") String status, Pageable pageable);

	public List<ObjectEntity> findByTypeAndStatus(@Param("type") String type, @Param("status") String status, Pageable pageable);

	public List<ObjectEntity> findByTypeAndStatusAndActiveTrue(@Param("type") String type, @Param("status") String status, Pageable pageable);

}