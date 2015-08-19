/**
 * Gesser Tecnologia LTDA - ME.
 * 19 de ago de 2015
 */
package org.magnum.mobilecloud.video.repo;

import org.magnum.mobilecloud.video.model.UserVideoRating;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Julio Gesser
 */
@Repository
public interface UserVideoRatingRepository extends CrudRepository<UserVideoRating, Long> {

	UserVideoRating findByUser(String user);

}
