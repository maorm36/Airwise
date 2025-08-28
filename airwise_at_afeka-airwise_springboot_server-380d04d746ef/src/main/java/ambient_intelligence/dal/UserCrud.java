package ambient_intelligence.dal;

import org.springframework.data.mongodb.repository.MongoRepository;
import ambient_intelligence.data.UserEntity;

public interface UserCrud extends MongoRepository<UserEntity, String>{}
