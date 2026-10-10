"""
CATEGORY A — DATA STRUCTURES CODING ASSIGNMENT
Solutions in Python 3
Problems: Prefix Sums, Sliding Window, Prefix Sum + Hash Map,
Binary Search, Spiral Traversal
"""

from bisect import bisect_left, bisect_right


# ------------------------------------------------------------
# Problem 1: Mall Footfall Range Report
# ------------------------------------------------------------
def footfallReport(visitors, queries):
    # prefix[i] stores the sum of visitors[0:i]
    prefix = [0]
    for value in visitors:
        prefix.append(prefix[-1] + value)

    # Inclusive range [start, end] = prefix[end + 1] - prefix[start]
    return [prefix[end + 1] - prefix[start] for start, end in queries]


# Example
visitors = [12, 7, 3, 9, 15, 4, 8]
queries = [(0, 2), (2, 5), (4, 6), (3, 3)]
print("Problem 1:", footfallReport(visitors, queries))
# Output: [22, 34, 27, 9]
# NOTE: The assignment PDF states 31 for query (2, 5), but
# 3 + 9 + 15 + 4 = 31. The correct output is [22, 31, 27, 9].
# (The implementation above returns the mathematically correct result.)


# ------------------------------------------------------------
# Problem 2: Longest Budget-Friendly Streak
# ------------------------------------------------------------
def longestStreak(costs, budget):
    left = 0
    total = 0
    best_length = 0
    best_start = -1

    for right, cost in enumerate(costs):
        total += cost

        # Non-negative costs mean shrinking the left side
        # cannot increase the sum.
        while total > budget and left <= right:
            total -= costs[left]
            left += 1

        length = right - left + 1
        if length > best_length:
            best_length = length
            best_start = left

    return (best_length, best_start)


print("Problem 2, sample 1:",
      longestStreak([4, 2, 1, 7, 3, 1, 2, 1, 5], 8))
# Output: (4, 4)
print("Problem 2, sample 2:", longestStreak([9, 10], 8))
# Output: (0, -1)


# ------------------------------------------------------------
# Problem 3: Net-Balance Period Counter
# ------------------------------------------------------------
def countPeriods(transactions, k):
    # Frequency of prefix sums seen so far.
    frequency = {0: 1}
    running_sum = 0
    count = 0

    for value in transactions:
        running_sum += value
        # If an earlier prefix sum is running_sum - k,
        # the subarray between them has sum exactly k.
        count += frequency.get(running_sum - k, 0)
        frequency[running_sum] = frequency.get(running_sum, 0) + 1

    return count


print("Problem 3, sample 1:",
      countPeriods([3, 4, -7, 1, 3, 3, 1, -4], 7))
# Output: 4
print("Problem 3, sample 2:", countPeriods([1, 2, 3], 10))
# Output: 0


# ------------------------------------------------------------
# Problem 4: Exam Score Band Counter
# ------------------------------------------------------------
def countInBand(scores, low, high):
    # First index with score >= low; first index with score > high.
    return bisect_right(scores, high) - bisect_left(scores, low)


scores = [35, 42, 42, 50, 58, 58, 58, 63, 71, 88]
print("Problem 4, sample 1:", countInBand(scores, 42, 58))
# Output: 6
print("Problem 4, sample 2:", countInBand(scores, 90, 100))
# Output: 0


# ------------------------------------------------------------
# Problem 5: Spiral Stock Audit Route
# ------------------------------------------------------------
def auditRoute(grid):
    if not grid or not grid[0]:
        return []

    top, bottom = 0, len(grid) - 1
    left, right = 0, len(grid[0]) - 1
    result = []

    while top <= bottom and left <= right:
        # Traverse top row, left to right.
        for col in range(left, right + 1):
            result.append(grid[top][col])
        top += 1

        # Traverse right column, top to bottom.
        for row in range(top, bottom + 1):
            result.append(grid[row][right])
        right -= 1

        # Traverse bottom row, right to left, if it remains.
        if top <= bottom:
            for col in range(right, left - 1, -1):
                result.append(grid[bottom][col])
            bottom -= 1

        # Traverse left column, bottom to top, if it remains.
        if left <= right:
            for row in range(bottom, top - 1, -1):
                result.append(grid[row][left])
            left += 1

    return result


grid = [[1, 2, 3, 4],
        [5, 6, 7, 8],
        [9, 10, 11, 12]]
print("Problem 5:", auditRoute(grid))
# Output: [1, 2, 3, 4, 8, 12, 11, 10, 9, 5, 6, 7]


# ------------------------------------------------------------
# Complexity summary
# ------------------------------------------------------------
# Problem 1:
# Naive: O(q*n) worst case; optimized: O(n+q) time, O(n) extra space.
# Prefix preprocessing is worthwhile when many range queries use the same array.
#
# Problem 2:
# Brute force checking all runs with re-summing: O(n^3) in a straightforward
# implementation (or O(n^2) with incremental sums); optimized: O(n) time,
# O(1) extra space. Non-negative values are essential because window sums
# behave predictably when moving the left/right boundary.
#
# Problem 3:
# Brute force summing every subarray: O(n^3) straightforward, O(n^2)
# with running sums; optimized: O(n) expected time, O(n) extra space.
# Negative values make a simple sliding window unreliable.
#
# Problem 4:
# Linear scan: O(n) time, O(1) extra space; binary-search solution:
# O(log n) time, O(1) extra space (excluding any library implementation details).
# Duplicates require finding boundary positions, not any arbitrary match.
#
# Problem 5:
# Time: O(m*n), output space: O(m*n); additional space excluding output: O(1).
