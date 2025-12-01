package to.yho.opp.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public void saveUser(UserVo vo) {
        repository.save(vo);
    }

    UserVo retrieve(UserVo vo) {
        return repository.findById(vo.getUserNo()).orElseThrow(IllegalArgumentException::new);
    }
}
