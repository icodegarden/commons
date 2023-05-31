package io.github.icodegarden.commons.redis.args;

import java.util.LinkedList;
import java.util.List;

import org.springframework.util.Assert;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@ToString
public class BitFieldArgs {

	private List<SubCommand> subCommands;

	public static interface SubCommand {
	}

	public BitFieldArgs addSubCommand(SubCommand subCommand) {
		if (subCommands == null) {
			subCommands = new LinkedList<>();
		}
		subCommands.add(subCommand);
		return this;
	}

	@Getter
	@Setter
	@ToString
	public static class Set implements SubCommand {

		private final BitFieldType bitFieldType;

		private final boolean bitOffset;

		private final int offset;

		private final long value;

		public Set(BitFieldType bitFieldType, boolean bitOffset, int offset, long value) {

			Assert.notNull(bitFieldType, "BitFieldType must not be null");
			Assert.isTrue(offset > -1, "Offset must be greater or equal to 0");

			this.bitFieldType = bitFieldType;
			this.bitOffset = bitOffset;
			this.offset = offset;
			this.value = value;
		}

	}

	@Getter
	@Setter
	@ToString
	public static class Get implements SubCommand {

		private final BitFieldType bitFieldType;

		private final boolean bitOffset;

		private final int offset;

		public Get(BitFieldType bitFieldType, boolean bitOffset, int offset) {

			Assert.notNull(bitFieldType, "BitFieldType must not be null");
			Assert.isTrue(offset > -1, "Offset must be greater or equal to 0");

			this.bitFieldType = bitFieldType;
			this.bitOffset = bitOffset;
			this.offset = offset;
		}

	}

	@Getter
	@Setter
	@ToString
	public static class IncrBy implements SubCommand {

		private final BitFieldType bitFieldType;

		private final boolean bitOffset;

		private final int offset;

		private final long value;

		public IncrBy(BitFieldType bitFieldType, boolean bitOffset, int offset, long value) {

			Assert.notNull(bitFieldType, "BitFieldType must not be null");
			Assert.isTrue(offset > -1, "Offset must be greater or equal to 0");

			this.bitFieldType = bitFieldType;
			this.bitOffset = bitOffset;
			this.offset = offset;
			this.value = value;
		}

	}

	@Getter
	@Setter
	@ToString
	public static class Overflow implements SubCommand {

		private final OverflowType overflowType;

		public Overflow(OverflowType overflowType) {

			Assert.notNull(overflowType, "OverflowType must not be null");
			this.overflowType = overflowType;
		}

	}

	/**
	 * Represents the overflow types for the {@code OVERFLOW} subcommand argument.
	 */
	public enum OverflowType {

		WRAP, SAT, FAIL;

//        public final byte[] bytes;
//
//        OverflowType() {
//            bytes = name().getBytes(StandardCharsets.US_ASCII);
//        }
//
//        @Override
//        public byte[] getBytes() {
//            return bytes;
//        }

	}

	/**
	 * Represents a bit field type with details about signed/unsigned and the number
	 * of bits.
	 */
	public static class BitFieldType {

		private final boolean signed;

		private final int bits;

		public BitFieldType(boolean signed, int bits) {

			Assert.isTrue(bits > 0, "Bits must be greater 0");

			if (signed) {
				Assert.isTrue(bits < 65, "Signed integers support only up to 64 bits");
			} else {
				Assert.isTrue(bits < 64, "Unsigned integers support only up to 63 bits");
			}

			this.signed = signed;
			this.bits = bits;
		}

		/**
		 *
		 * @return {@code true} if the bitfield type is signed.
		 */
		public boolean isSigned() {
			return signed;
		}

		/**
		 *
		 * @return number of bits.
		 */
		public int getBits() {
			return bits;
		}

		private String asString() {
			return (signed ? "i" : "u") + bits;
		}

		@Override
		public String toString() {
			return asString();
		}

	}

	/**
	 * Represents a bit field offset. See also
	 * <a href="https://redis.io/commands/bitfield#bits-and-positional-offsets">Bits
	 * and positional offsets</a>
	 *
	 * @since 4.3
	 */
	public static class Offset {

		private final boolean multiplyByTypeWidth;

		private final int offset;

		private Offset(boolean multiplyByTypeWidth, int offset) {

			this.multiplyByTypeWidth = multiplyByTypeWidth;
			this.offset = offset;
		}

		/**
		 * @return {@code true} if the offset should be multiplied by integer width that
		 *         is represented with a leading hash ( {@code #}) when constructing the
		 *         command
		 */
		public boolean isMultiplyByTypeWidth() {
			return multiplyByTypeWidth;
		}

		/**
		 *
		 * @return the offset.
		 */
		public int getOffset() {
			return offset;
		}

		@Override
		public String toString() {
			return (multiplyByTypeWidth ? "#" : "") + offset;
		}

	}

}
