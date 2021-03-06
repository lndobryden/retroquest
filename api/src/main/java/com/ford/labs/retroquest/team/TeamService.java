/*
 * Copyright © 2018 Ford Motor Company
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ford.labs.retroquest.team;

import com.ford.labs.retroquest.actionitem.ActionItem;
import com.ford.labs.retroquest.actionitem.ActionItemRepository;
import com.ford.labs.retroquest.columntitle.ColumnTitle;
import com.ford.labs.retroquest.columntitle.ColumnTitleRepository;
import com.ford.labs.retroquest.exception.BoardDoesNotExistException;
import com.ford.labs.retroquest.exception.PasswordInvalidException;
import com.ford.labs.retroquest.exception.TeamAlreadyHasPasswordException;
import com.ford.labs.retroquest.thought.Thought;
import com.ford.labs.retroquest.thought.ThoughtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {
    @Autowired
    private ThoughtRepository thoughtRepository;

    @Autowired
    private ActionItemRepository actionItemRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ColumnTitleRepository columnTitleRepository;

    public String convertTeamNameToURI(String teamName) {
        return teamName.toLowerCase().replace(" ", "-");
    }

    public CsvFile buildCsvFileFromTeam(String team) {
        List<Thought> thoughts = thoughtRepository.findAllByTeamIdOrderByTopic(team);
        List<ActionItem> actionItems = actionItemRepository.findAllByTeamId(team);
        return new CsvFile(team, thoughts, actionItems);
    }

    public Team createNewTeam(RequestedTeam requestedTeam){
        String requestedName = requestedTeam.getName();

        Team teamEntity = new Team();
        teamEntity.setName(requestedName);
        teamEntity.setUri(convertTeamNameToURI(requestedName));

        String encryptedPassword = passwordEncoder.encode(requestedTeam.getPassword());
        teamEntity.setPassword(encryptedPassword);

        teamEntity = teamRepository.save(teamEntity);

        ColumnTitle happyColumnTitle = new ColumnTitle();
        happyColumnTitle.setTeamId(teamEntity.getUri());
        happyColumnTitle.setTopic("happy");
        happyColumnTitle.setTitle("Happy");

        ColumnTitle confusedColumnTitle = new ColumnTitle();
        confusedColumnTitle.setTeamId(teamEntity.getUri());
        confusedColumnTitle.setTopic("confused");
        confusedColumnTitle.setTitle("Confused");

        ColumnTitle unhappyColumnTitle = new ColumnTitle();
        unhappyColumnTitle.setTeamId(teamEntity.getUri());
        unhappyColumnTitle.setTopic("unhappy");
        unhappyColumnTitle.setTitle("Sad");

        columnTitleRepository.save(happyColumnTitle);
        columnTitleRepository.save(confusedColumnTitle);
        columnTitleRepository.save(unhappyColumnTitle);

        return teamEntity;
    }

    public void setTeamPassword(String teamUri, SetPasswordRequest requestedPassword) {
        Team savedTeam = teamRepository.findOne(teamUri);
        if(savedTeam.getPassword() == null) {
            String password = requestedPassword.getPassword();
            String encryptedPassword = passwordEncoder.encode(password);

            savedTeam.setPassword(encryptedPassword);

            teamRepository.save(savedTeam);
        }
        else {
            throw new TeamAlreadyHasPasswordException();
        }
    }

    public Team login(RequestedTeam requestedTeam) {
        Team savedTeam = teamRepository.findTeamByName(requestedTeam.getName());

        if(savedTeam == null) {
            throw new BoardDoesNotExistException();
        }

        if(requestedTeam.getPassword() == null || !passwordEncoder.matches(requestedTeam.getPassword(), savedTeam.getPassword())) {
            throw new PasswordInvalidException();
        }

        return savedTeam;
    }
}
