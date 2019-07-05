package io.usoamic.testcli

import io.usoamic.cli.core.Usoamic
import io.usoamic.cli.enum.IdeaStatus
import io.usoamic.cli.enum.VoteType
import io.usoamic.testcli.other.TestConfig
import org.junit.jupiter.api.Test
import org.web3j.protocol.Web3j
import java.math.BigInteger
import javax.inject.Inject
import kotlin.random.Random

class IdeasTest {
    @Inject
    lateinit var usoamic: Usoamic

    @Inject
    lateinit var web3j: Web3j

    init {
        BaseUnitTest.componentTest.inject(this)
    }

    @Test
    fun addIdeaTest() {
        val description = generateIdeaDescription()
        val txHash = usoamic.addIdea(TestConfig.PASSWORD, description)

        usoamic.waitTransactionReceipt(txHash) {
            val ideaId = usoamic.getLastIdeaId()
            val idea = usoamic.getIdea(ideaId)
            assert(idea.isExist)
            assert(idea.author == usoamic.account.address)
            assert(idea.description == description)
            assert(idea.ideaId == ideaId)
            assert(idea.ideaStatus == IdeaStatus.DISCUSSION)
        }
    }

    @Test
    fun supportIdea() {
        voteForIdeaTest(VoteType.SUPPORT)
    }

    @Test
    fun abstainIdea() {
        voteForIdeaTest(VoteType.ABSTAIN)
    }

    @Test
    fun againstIdea() {
        voteForIdeaTest(VoteType.AGAINST)
    }

    private fun voteForIdeaTest(voteType: VoteType) {
        val ideaTxHash = usoamic.addIdea(TestConfig.PASSWORD, generateIdeaDescription())

        usoamic.waitTransactionReceipt(ideaTxHash) {
            val ideaId = usoamic.getLastIdeaId()
            val comment = ("Comment #" + Random.nextInt())

            println("IdeaId: $ideaId; Comment: $comment")

            val voteTxHash = when (voteType) {
                VoteType.SUPPORT -> usoamic.supportIdea(TestConfig.PASSWORD, ideaId, comment)
                VoteType.ABSTAIN -> usoamic.abstainIdea(TestConfig.PASSWORD, ideaId, comment)
                VoteType.AGAINST -> usoamic.againstIdea(TestConfig.PASSWORD, ideaId, comment)
            }
            usoamic.waitTransactionReceipt(voteTxHash) {
                val idea = usoamic.getIdea(ideaId)

                when (voteType) {
                    VoteType.SUPPORT -> {
                        assert(idea.numberOfSupporters > BigInteger.ZERO)
                    }
                    VoteType.ABSTAIN -> {
                        assert(idea.numberOfAbstained > BigInteger.ZERO)
                    }
                    VoteType.AGAINST -> {
                        assert(idea.numberOfVotedAgainst > BigInteger.ZERO)
                    }
                }
                assert(idea.numberOfParticipants > BigInteger.ZERO)
            }
        }
    }

    private fun generateIdeaDescription(): String {
        return "Idea #" + Random.nextInt()
    }
}