# Resource post & Download link
https://www.spigotmc.org/resources/79584/

# What is LiteSignIn ?
This is a plug-in for recording player check-in data, and rewards players for check-in. The content of the reward can be completely customized, it can trigger commands, message prompts, item rewards, etc. when signing in. It supports GUI mode, just like a calendar, it is very simple to use.

# Current Features
- Remind to sign in after joining the game  
- Sign in using command  
- Have a GUI like a calendar  
- Support viewing history sign-in records  
- Multi-language support  
- Sign in reward function  
- Sign in reward task sequence  
- Support multiple reward modes such as rewards for continuous check-in, total rewards for check-in, rewards for check-in on a specified date, etc.  
- Sign in leaderboard.  
- Sign-in reminder at join (or automatic sign-in)  
- Give sign-in rewards directly (use /signin reward)  
- Support PlaceholderAPI  
- Support MySQL storage  
- Support SQLite storage  
- Support the use of retroactive cards for re sign-in  
- Support multiple permission groups, users in different groups will enjoy different rewards  
- Support sign-in ranking function  
- Database backup  
- Database rollback(Need to use with backup function)  
- All permission nodes, Messages, and GUI settings can be customized (But you have to remember that this is not a custom menu plugin)  
- Wooden sign scripts  
- Folia support  
And more...  

# Commands
| Command | Function |
|:- |:- |
|/signin help | View command help.
|/signin reload | Reload configuration files.
|/signin gui [month] | Open the Sign In GUI menu.
|/signin click [Date or playerName] [playerName] | Sign in now, or sign in for historical date.
|/signin save | Save data for all online players.
|/signin leaderboard [Date] [Page] | View the sign-in leaderboard for today or a specified date.
|/signin database [backup/rollback/confirm] | Database managements
|/signin info [playerName] | View player's information.
|/signin itemcollection | Collection of management items.
|/signin reward | Directly give the player a sign-in reward.

# PlaceholderAPI's placeholders
| Placeholder | Action |
|:- |:- |
|%litesignin_signed-in% | Did you sign in today?
|%litesignin_group% | Return the player's group.
|%litesignin_cards_amount% | Return number of retroactive cards.
|%litesignin_statistics% | Return the cumulative numbers of user sign in.
|%litesignin_continuous% | Return the number of continuous sign in.
|%litesignin_statistics_of_month% | Return the cumulative numbers of user sign in of this month.
|%litesignin_continuous_of_month% | Return the number of continuous sign in of this month.
|%litesignin_last_year% | Returns the year of the latest sign-in.
|%litesignin_last_month% | Returns the month of the latest sign-in.
|%litesignin_last_day% | Returns the day of the latest sign-in.
|%litesignin_last_hour% | Returns the hour of the latest sign-in.
|%litesignin_last_minute% | Returns the minute of the latest sign-in.
|%litesignin_last_second% | Returns the second of the latest sign-in.

Another placeholders:  
%litesignin_random_[minimum]_[maximum]% Returns a random value within a specified range of numbers. (This is a placeholder for a gadget type.)

[![BigImage](https://bstats.org/signatures/bukkit/LiteSignIn.svg)](https://bstats.org/plugin/bukkit/LiteSignIn)
