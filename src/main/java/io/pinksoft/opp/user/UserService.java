package io.pinksoft.opp.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public void saveUser(UserVo vo) {
        User user = new User();
        user.setUserNm(vo.getUserNm());
        repository.save(user);
    }

    public UserVo retrieve(UserVo vo) {
        User user = repository.findById(vo.getUserNo()).orElseThrow(IllegalArgumentException::new);
        UserVo result = new UserVo();
        result.setUserNo(user.getUserNo());
        result.setUserNm(user.getUserNm());
        return result;
    }
}
