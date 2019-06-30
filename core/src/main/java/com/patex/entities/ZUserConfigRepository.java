package com.patex.entities;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Alexey on 25.03.2017.
 */
@Repository
public interface ZUserConfigRepository extends CrudRepository<ZUserConfig, String> {

    Optional<ZUserConfig> findByTelegramChatId(long telegramChatId);

}
